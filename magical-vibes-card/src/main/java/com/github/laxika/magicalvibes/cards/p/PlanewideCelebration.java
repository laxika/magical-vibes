package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "172")
public class PlanewideCelebration extends Card {

    private static final String[] MODE_LABELS = {
            "Create a 2/2 Citizen token that's all colors",
            "Return target permanent card from your graveyard to your hand",
            "Proliferate",
            "You gain 4 life"
    };

    public PlanewideCelebration() {
        CardEffect createCitizen = new CreateTokenEffect(
                "Citizen", 2, 2, null,
                Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.CITIZEN));
        CardEffect proliferate = new ProliferateEffect();
        CardEffect gainLife = new GainLifeEffect(4);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(buildModeChoices(
                createCitizen, proliferate, gainLife)));
    }

    private static List<ChooseOneEffect.ChooseOneOption> buildModeChoices(
            CardEffect createCitizen, CardEffect proliferate, CardEffect gainLife) {
        List<ChooseOneEffect.ChooseOneOption> choices = new ArrayList<>();
        for (int first = 0; first < MODE_LABELS.length; first++) {
            for (int second = first; second < MODE_LABELS.length; second++) {
                for (int third = second; third < MODE_LABELS.length; third++) {
                    for (int fourth = third; fourth < MODE_LABELS.length; fourth++) {
                        int[] counts = new int[MODE_LABELS.length];
                        counts[first]++;
                        counts[second]++;
                        counts[third]++;
                        counts[fourth]++;

                        List<CardEffect> effects = new ArrayList<>();
                        addRepeated(effects, createCitizen, counts[0]);
                        if (counts[1] > 0) {
                            effects.add(new ReturnTargetCardsFromGraveyardToHandEffect(
                                    new CardIsPermanentPredicate(), counts[1], null,
                                    false, false, counts[1], false, Set.of(), false));
                        }
                        addRepeated(effects, proliferate, counts[2]);
                        addRepeated(effects, gainLife, counts[3]);
                        choices.add(new ChooseOneEffect.ChooseOneOption(modeLabel(counts), effects));
                    }
                }
            }
        }
        return choices;
    }

    private static void addRepeated(List<CardEffect> effects, CardEffect effect, int count) {
        for (int i = 0; i < count; i++) {
            effects.add(effect);
        }
    }

    private static String modeLabel(int[] counts) {
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 0) {
                continue;
            }
            if (label.length() > 0) {
                label.append("; ");
            }
            if (counts[i] > 1) {
                label.append(counts[i]).append("x ");
            }
            label.append(MODE_LABELS[i]);
        }
        return label.toString();
    }
}
