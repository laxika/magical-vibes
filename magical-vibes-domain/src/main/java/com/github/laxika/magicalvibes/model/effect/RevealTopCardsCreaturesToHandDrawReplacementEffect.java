package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement capability: while a permanent carrying this effect is on the battlefield, if
 * its controller would draw a card, instead they reveal the top {@link #revealCount()} cards of their
 * library, put every revealed creature card into their hand, and put the rest on the bottom of their
 * library in any order. The draw is fully replaced — the revealed creatures are not "drawn" (no draw
 * triggers, no empty-library loss when nothing can be revealed). Sages of the Anima ({@code revealCount()
 * == 3}). Detected in {@code DrawService.resolveDrawCard}; read as a fact, never {@code instanceof}-ed on
 * a concrete type.
 */
public interface RevealTopCardsCreaturesToHandDrawReplacementEffect extends CardEffect {

    int revealCount();
}
