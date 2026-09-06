package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaEqualToEnchantedPermanentManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddManaEqualToEnchantedPermanentManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaEqualToEnchantedPermanentManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }

        Permanent enchanted = aura == null || aura.getAttachedTo() == null
                ? entry.getAttachedPermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            enchanted = entry.getAttachedPermanentSnapshot();
        }
        if (enchanted == null) {
            return;
        }

        ManaCost manaCost = enchanted.getCard().getParsedManaCost();
        if (manaCost == null) {
            return;
        }
        List<Set<ManaColor>> choices = manaCost.getManaProductionChoices();
        if (choices.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        boolean fromCreature = gameQueryService.isCreature(gameData, aura);
        List<Set<ManaColor>> selectableChoices = choices.stream()
                .filter(choice -> choice.size() > 1)
                .toList();
        for (Set<ManaColor> choice : choices) {
            if (choice.size() == 1) {
                addMana(gameData, controllerId, choice.iterator().next(), fromCreature);
            }
        }

        if (!selectableChoices.isEmpty()) {
            beginChoice(gameData, controllerId, selectableChoices, fromCreature);
        }
    }

    private void beginChoice(GameData gameData, UUID controllerId, List<Set<ManaColor>> choices,
                             boolean fromCreature) {
        ChoiceContext.EnchantedManaCostChoice context =
                new ChoiceContext.EnchantedManaCostChoice(controllerId, choices, fromCreature);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, context,
                choices.getFirst().stream().map(Enum::name).toList(),
                "Choose a color of mana to add."));
    }

    private void addMana(GameData gameData, UUID playerId, ManaColor color, boolean fromCreature) {
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId, color);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        pool.add(effectiveColor, 1);
        if (fromCreature) {
            pool.addCreatureMana(effectiveColor, 1);
        }
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds one " + effectiveColor.getCode() + "."));
    }
}
