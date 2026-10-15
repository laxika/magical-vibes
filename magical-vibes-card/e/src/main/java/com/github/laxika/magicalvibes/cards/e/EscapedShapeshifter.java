package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasProtectionFromColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

/**
 * Each of the four abilities is checked independently, and "protection from any color" is really
 * five separate checks — one per color — so an opponent's white-protected creature only ever
 * grants protection from white.
 */
@CardRegistration(set = "TMP", collectorNumber = "62")
public class EscapedShapeshifter extends Card {

    private static final String CARD_NAME = "Escaped Shapeshifter";

    public EscapedShapeshifter() {
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE)) {
            addEffect(EffectSlot.STATIC, new ConditionalEffect(
                    new OpponentControlsPermanent(otherCreatureMatching(new PermanentHasKeywordPredicate(keyword))),
                    new GrantKeywordEffect(keyword, GrantScope.SELF)));
        }
        for (CardColor color : CardColor.values()) {
            addEffect(EffectSlot.STATIC, new ConditionalEffect(
                    new OpponentControlsPermanent(
                            otherCreatureMatching(new PermanentHasProtectionFromColorPredicate(color))),
                    new ProtectionFromColorsEffect(Set.of(color))));
        }
    }

    /**
     * The name exclusion comes first so that a mirrored Escaped Shapeshifter is rejected before
     * the ability check is asked of it.
     */
    private static PermanentPredicate otherCreatureMatching(PermanentPredicate abilityCheck) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentNamedPredicate(CARD_NAME)),
                new PermanentIsCreaturePredicate(),
                abilityCheck));
    }
}
