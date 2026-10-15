package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "306")
@CardRegistration(set = "MB2", collectorNumber = "542")
public class CleaverBlow extends Card {

    private static final int NONBLACK = 1;
    private static final int SMALL = 1 << 1;
    private static final int OPPONENT = 1 << 2;
    private static final int EACH_CONTROLLER = 1 << 3;
    private static final int LOSE_LIFE = 1 << 4;
    private static final int TAPPED = 1 << 5;

    private static final List<String> BRACKETED_TEXT = List.of(
            "nonblack",
            "with mana value 3 or less",
            "an opponent controls",
            "and its controller each",
            "and lose 2 life",
            "tapped");

    public CleaverBlow() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(buildModes()));
    }

    private static List<ChooseOneEffect.ChooseOneOption> buildModes() {
        List<ChooseOneEffect.ChooseOneOption> modes = new ArrayList<>(1 << BRACKETED_TEXT.size());
        for (int mask = 0; mask < (1 << BRACKETED_TEXT.size()); mask++) {
            PermanentPredicate targetPredicate = targetPredicate(mask);
            List<CardEffect> effects = new ArrayList<>();
            effects.add(new DestroyTargetPermanentEffect(targetPredicate));
            effects.add(new DrawCardEffect());
            if ((mask & EACH_CONTROLLER) == 0) {
                effects.add(new TargetPermanentControllerDrawsCardEffect());
            }
            if ((mask & LOSE_LIFE) == 0) {
                effects.add(new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER));
                if ((mask & EACH_CONTROLLER) == 0) {
                    effects.add(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
                }
            }
            effects.add(CreateTokenEffect.whiteSpirit(1).withTapped((mask & TAPPED) == 0));

            modes.add(new ChooseOneEffect.ChooseOneOption(
                    modeLabel(mask), effects,
                    new PermanentPredicateTargetFilter(targetPredicate, targetMessage(mask)))
                    .withManaCost("{" + (1 + Integer.bitCount(mask)) + "}{B}"));
        }
        return modes;
    }

    private static PermanentPredicate targetPredicate(int mask) {
        List<PermanentPredicate> predicates = new ArrayList<>();
        predicates.add(new PermanentIsCreaturePredicate());
        if ((mask & NONBLACK) == 0) {
            predicates.add(new PermanentNotPredicate(
                    new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
        }
        if ((mask & SMALL) == 0) {
            predicates.add(new PermanentMaxManaValuePredicate(3));
        }
        if ((mask & OPPONENT) == 0) {
            predicates.add(new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()));
        }
        return new PermanentAllOfPredicate(predicates);
    }

    private static String modeLabel(int mask) {
        List<String> removed = new ArrayList<>();
        for (int bit = 0; bit < BRACKETED_TEXT.size(); bit++) {
            if ((mask & (1 << bit)) != 0) {
                removed.add("[" + BRACKETED_TEXT.get(bit) + "]");
            }
        }
        return removed.isEmpty()
                ? "Cleaver Blow"
                : "Cleaver Blow - remove " + String.join(", ", removed);
    }

    private static String targetMessage(int mask) {
        List<String> restrictions = new ArrayList<>();
        restrictions.add("a creature");
        if ((mask & NONBLACK) == 0) {
            restrictions.add("that isn't black");
        }
        if ((mask & SMALL) == 0) {
            restrictions.add("with mana value 3 or less");
        }
        if ((mask & OPPONENT) == 0) {
            restrictions.add("an opponent controls");
        }
        return "Target must be " + String.join(" ", restrictions);
    }
}
