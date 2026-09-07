package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseControlledArtifactOrCreatureTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.ArrayList;
import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "68")
public class SeasonOfWeaving extends Card {

    public SeasonOfWeaving() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(buildModes()));
    }

    private static List<ChooseOneEffect.ChooseOneOption> buildModes() {
        List<ChooseOneEffect.ChooseOneOption> modes = new ArrayList<>();
        for (int returns = 0; returns <= 1; returns++) {
            for (int copies = 0; copies <= 2; copies++) {
                for (int draws = 0; draws <= 5 - returns * 3 - copies * 2; draws++) {
                    modes.add(mode(draws, copies, returns));
                }
            }
        }
        return modes;
    }

    private static ChooseOneEffect.ChooseOneOption mode(int draws, int copies, int returns) {
        List<CardEffect> effects = new ArrayList<>();
        for (int i = 0; i < draws; i++) {
            effects.add(new DrawCardEffect());
        }
        for (int i = 0; i < copies; i++) {
            effects.add(new ChooseControlledArtifactOrCreatureTokenCopyEffect());
        }
        if (returns == 1) {
            effects.add(ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                    new PermanentNotPredicate(new PermanentIsLandPredicate()),
                    new PermanentNotPredicate(new PermanentIsTokenPredicate())))));
        }

        return new ChooseOneEffect.ChooseOneOption(modeLabel(draws, copies, returns), effects);
    }

    private static String modeLabel(int draws, int copies, int returns) {
        if (draws == 0 && copies == 0 && returns == 0) {
            return "Choose no modes";
        }

        List<String> parts = new ArrayList<>();
        if (draws > 0) {
            parts.add("Draw " + draws + " card" + (draws == 1 ? "" : "s"));
        }
        if (copies > 0) {
            parts.add("Create " + copies + " token " + (copies == 1 ? "copy" : "copies")
                    + " of an artifact or creature");
        }
        if (returns > 0) {
            parts.add("Return each nonland, nontoken permanent to its owner's hand");
        }
        return String.join("; ", parts);
    }
}
