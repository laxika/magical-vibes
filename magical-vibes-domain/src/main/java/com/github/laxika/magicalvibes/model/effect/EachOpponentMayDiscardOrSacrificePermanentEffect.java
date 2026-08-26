package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent may discard a card or sacrifice a permanent. An opponent who declines both
 * options, or cannot perform either option, is dealt the specified damage.
 *
 * @param damageIfNeither damage dealt to an opponent who neither discards nor sacrifices
 * @param choiceStage the optional choice currently being offered
 */
public record EachOpponentMayDiscardOrSacrificePermanentEffect(
        int damageIfNeither, ChoiceStage choiceStage) implements CardEffect {

    public EachOpponentMayDiscardOrSacrificePermanentEffect(int damageIfNeither) {
        this(damageIfNeither, ChoiceStage.DISCARD);
    }

    public EachOpponentMayDiscardOrSacrificePermanentEffect forSacrificeChoice() {
        return new EachOpponentMayDiscardOrSacrificePermanentEffect(damageIfNeither, ChoiceStage.SACRIFICE);
    }

    public enum ChoiceStage {
        DISCARD,
        SACRIFICE
    }
}
