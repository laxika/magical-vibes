package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma — Reveal the top {@code count} cards of your library. For each {@code color} mana symbol in
 * the mana costs of the revealed cards, the target player mills a card. Then put the revealed cards
 * on the bottom of your library in any order.
 *
 * <p>Hybrid and Phyrexian symbols of {@code color} each count once (see
 * {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic and {X} never
 * count. Used by Sanity Grinding ({@code count}=10, {@code color}=BLUE).
 *
 * @param count the number of cards to reveal from the top of the controller's library
 * @param color the mana color whose symbols among the revealed cards determine the mill count
 */
public record RevealTopCardsMillTargetByColorSymbolsEffect(int count, ManaColor color) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
