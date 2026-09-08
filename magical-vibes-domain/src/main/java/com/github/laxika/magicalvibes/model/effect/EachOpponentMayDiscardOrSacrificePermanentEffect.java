package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Each opponent may discard a card or sacrifice a permanent. An opponent who declines both
 * options, or cannot perform either option, is dealt the specified damage.
 *
 * @param damageIfNeither amount of damage dealt to an opponent who neither discards nor sacrifices
 * @param sacrificeFilter filter for permanents that may be sacrificed
 * @param choiceStage the optional choice currently being offered
 */
public record EachOpponentMayDiscardOrSacrificePermanentEffect(
        DynamicAmount damageIfNeither, PermanentPredicate sacrificeFilter, ChoiceStage choiceStage)
        implements CardEffect {

    public EachOpponentMayDiscardOrSacrificePermanentEffect(int damageIfNeither) {
        this(new Fixed(damageIfNeither), new PermanentTruePredicate(), ChoiceStage.DISCARD);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect(int damageIfNeither,
                                                              ChoiceStage choiceStage) {
        this(new Fixed(damageIfNeither), new PermanentTruePredicate(), choiceStage);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect(DynamicAmount damageIfNeither) {
        this(damageIfNeither, new PermanentTruePredicate(), ChoiceStage.DISCARD);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect(int damageIfNeither,
                                                              PermanentPredicate sacrificeFilter) {
        this(new Fixed(damageIfNeither), sacrificeFilter, ChoiceStage.DISCARD);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect(DynamicAmount damageIfNeither,
                                                              PermanentPredicate sacrificeFilter) {
        this(damageIfNeither, sacrificeFilter, ChoiceStage.DISCARD);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect forSacrificeChoice() {
        return new EachOpponentMayDiscardOrSacrificePermanentEffect(
                damageIfNeither, sacrificeFilter, ChoiceStage.SACRIFICE);
    }

    public enum ChoiceStage {
        DISCARD,
        SACRIFICE
    }
}
