package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.ArrayList;
import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "112")
public class SeasonOfLoss extends Card {

    public SeasonOfLoss() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(buildModes()));
    }

    private static List<ChooseOneEffect.ChooseOneOption> buildModes() {
        List<ChooseOneEffect.ChooseOneOption> modes = new ArrayList<>();
        for (int sacrifices = 0; sacrifices <= 5; sacrifices++) {
            for (int draws = 0; draws <= (5 - sacrifices) / 2; draws++) {
                for (int lifeLosses = 0; lifeLosses <= (5 - sacrifices - 2 * draws) / 3; lifeLosses++) {
                    modes.add(mode(sacrifices, draws, lifeLosses));
                }
            }
        }
        return modes;
    }

    private static ChooseOneEffect.ChooseOneOption mode(int sacrifices, int draws, int lifeLosses) {
        List<CardEffect> effects = new ArrayList<>();
        for (int i = 0; i < sacrifices; i++) {
            effects.add(new SacrificePermanentsEffect(1,
                    new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                    SacrificeRecipient.EACH_PLAYER));
        }
        for (int i = 0; i < draws; i++) {
            effects.add(new DrawCardEffect(new CreatureDeathsThisTurn(CountScope.CONTROLLER)));
        }
        for (int i = 0; i < lifeLosses; i++) {
            effects.add(new LoseLifeEffect(
                    new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                    LoseLifeRecipient.EACH_OPPONENT));
        }
        return new ChooseOneEffect.ChooseOneOption(modeLabel(sacrifices, draws, lifeLosses), effects);
    }

    private static String modeLabel(int sacrifices, int draws, int lifeLosses) {
        if (sacrifices == 0 && draws == 0 && lifeLosses == 0) {
            return "Choose no modes";
        }

        List<String> parts = new ArrayList<>();
        if (sacrifices > 0) {
            parts.add("Each player sacrifices a creature " + repeatLabel(sacrifices));
        }
        if (draws > 0) {
            parts.add("Draw a card for each creature that died under your control " + repeatLabel(draws));
        }
        if (lifeLosses > 0) {
            parts.add("Each opponent loses life equal to the number of creature cards in your graveyard "
                    + repeatLabel(lifeLosses));
        }
        return String.join("; ", parts);
    }

    private static String repeatLabel(int count) {
        return count == 1 ? "once" : count + " times";
    }
}
