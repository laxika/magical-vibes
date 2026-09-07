package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenMayCopyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("returnTargetPermanentToHandThenMayCopyMayEffectHandler")
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandThenMayCopyEffectHandler implements MayEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandThenMayCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID controllerId = ability.controllerId();
        List<UUID> landIds = maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, controllerId, new PermanentIsLandPredicate());
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (landIds.isEmpty() || pendingEntry == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry spellSnapshot = new StackEntry(pendingEntry);
        MayEffect copyChoice = new MayEffect(
                new CopyControllerCastSpellEffect(spellSnapshot, controllerId),
                "Copy this spell?",
                null,
                MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER);

        pendingEntry.setTargetId(controllerId);
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificePermanentThen(
                        controllerId, ability.sourceCard(), copyChoice, false));
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                landIds,
                ability.sourceCard().getName() + " - Choose a land to sacrifice.");
    }
}
