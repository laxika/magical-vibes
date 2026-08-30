package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "29")
public class SeasonOfTheBurrow extends Card {

    public SeasonOfTheBurrow() {
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(buildModes()));
    }

    private static List<ChooseOneEffect.ChooseOneOption> buildModes() {
        List<ChooseOneEffect.ChooseOneOption> modes = new ArrayList<>();
        for (int returns = 0; returns <= 1; returns++) {
            for (int exiles = 0; exiles <= 2; exiles++) {
                for (int rabbits = 0; rabbits <= 5 - returns * 3 - exiles * 2; rabbits++) {
                    modes.add(mode(rabbits, exiles, returns));
                }
            }
        }
        return modes;
    }

    private static ChooseOneEffect.ChooseOneOption mode(int rabbits, int exiles, int returns) {
        List<CardEffect> effects = new ArrayList<>();
        for (int i = 0; i < rabbits; i++) {
            effects.add(new CreateTokenEffect(
                    1, "Rabbit", 1, 1, CardColor.WHITE,
                    List.of(CardSubtype.RABBIT), Set.of(), Set.of()));
        }
        for (int i = 0; i < exiles; i++) {
            effects.add(new ExileTargetPermanentThenEffect(
                    new DrawCardEffect(), ThenEffectRecipient.TARGET_CONTROLLER));
        }
        if (returns == 1) {
            effects.add(ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .filter(new CardAllOfPredicate(List.of(
                            new CardIsPermanentPredicate(),
                            new CardMaxManaValuePredicate(3))))
                    .targetGraveyard(true)
                    .grantIndestructible(true)
                    .build());
        }

        String label = modeLabel(rabbits, exiles, returns);
        if (exiles == 0 && returns == 0) {
            return new ChooseOneEffect.ChooseOneOption(label, effects);
        }
        List<TargetFilter> targetFilters = new ArrayList<>();
        for (int i = 0; i < exiles; i++) {
            targetFilters.add(TargetFilters.nonlandPermanent());
        }
        if (returns == 1) {
            targetFilters.add(new GraveyardCardPredicateTargetFilter(
                    new CardAllOfPredicate(List.of(
                            new CardIsPermanentPredicate(),
                            new CardMaxManaValuePredicate(3))),
                    GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        }
        return new ChooseOneEffect.ChooseOneOption(label, effects, targetFilters);
    }

    private static String modeLabel(int rabbits, int exiles, int returns) {
        List<String> parts = new ArrayList<>();
        if (rabbits > 0) {
            parts.add("Create " + rabbits + " Rabbit token" + (rabbits == 1 ? "" : "s"));
        }
        if (exiles > 0) {
            parts.add("Exile " + exiles + " nonland permanent" + (exiles == 1 ? "" : "s"));
        }
        if (returns > 0) {
            parts.add("Return a permanent card");
        }
        return parts.isEmpty() ? "Choose no modes" : String.join("; ", parts);
    }
}
