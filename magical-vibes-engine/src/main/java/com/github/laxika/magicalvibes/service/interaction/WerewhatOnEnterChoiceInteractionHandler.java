package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.WerewhatSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WerewhatOnEnterChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.WerewhatOnEnterChoice> {

    private final WerewhatSupport support;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InputCompletionService inputCompletionService;
    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Autowired
    public WerewhatOnEnterChoiceInteractionHandler(WerewhatSupport support,
                                                    @Lazy BattlefieldEntryService battlefieldEntryService,
                                                    InputCompletionService inputCompletionService,
                                                    @Lazy DrawService drawService,
                                                    GameQueryService gameQueryService,
                                                    @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.support = support;
        this.battlefieldEntryService = battlefieldEntryService;
        this.inputCompletionService = inputCompletionService;
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<PendingInteraction.WerewhatOnEnterChoice> handledType() {
        return PendingInteraction.WerewhatOnEnterChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.WerewhatOnEnterChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one valid creature card");
        }

        gameData.interaction.clearAwaitingInput();
        if (!chosenIds.isEmpty()) {
            Card offered = support.findOfferedCard(gameData, interaction, chosenIds.getFirst());
            if (offered != null && offered.getBackFaceCard() != null) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.WerewhatBackFaceChoice(
                        interaction.playerId(), interaction, offered.getId(),
                        List.of(offered.getName(), offered.getBackFaceCard().getName())));
                return;
            }
            WerewhatSupport.ChoiceResult result = support.applyChoice(gameData, interaction,
                    chosenIds.getFirst());
            if (result.fromHand()) {
                drawService.resolveDrawCard(gameData, interaction.controllerId());
            }
        }
        finishEntering(gameData, interaction);
    }

    /** Runs the entering permanent's enters abilities, using whichever face it entered with. */
    public void finishEntering(GameData gameData, PendingInteraction.WerewhatOnEnterChoice interaction) {
        Permanent entering = gameQueryService.findPermanentById(gameData, interaction.enteringPermanentId());
        Card enteringCard = entering != null && entering.isTransformed() ? entering.getCard() : interaction.card();
        battlefieldEntryService.processCreatureETBEffects(gameData, interaction.controllerId(),
                enteringCard, interaction.targetId(), interaction.wasCastFromHand(),
                interaction.etbMode(), interaction.kicked(), interaction.targetIds());
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    public void drawForHandExile(GameData gameData, PendingInteraction.WerewhatOnEnterChoice interaction) {
        drawService.resolveDrawCard(gameData, interaction.controllerId());
    }
}
