package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns one specific exiled card to the battlefield under its owner's control.
 *
 * <p>Written into a token blueprint's {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_DEATH}
 * slot for "Return this card to the battlefield under its owner's control when that token dies"
 * (Tatsumasa, the Dragon's Fang). The card is authored with a {@code null} id — the token-creation
 * handler bakes in the id of the card that created the token, which is the card exiled to pay the
 * activation cost. Nothing happens if that card has since left exile.
 *
 * <p>When {@code underAbilityController} is set the card instead returns under the control of the
 * controller of the resolving ability (CR 400.7-style default for abilities that say only "return
 * the exiled card to the battlefield", e.g. Timothar, Baron of Bats's Bat token). That form is also a
 * linked ability: it only returns a card whose exile entry names the resolving ability's source, so a
 * copy of the token returns nothing.
 *
 * @param exiledCardId the exiled card to return, or {@code null} to be bound at token creation
 * @param enterTapped whether the card enters tapped
 * @param underAbilityController whether the card enters under the ability controller's control
 *                               rather than its owner's
 */
public record ReturnExiledCardToBattlefieldUnderOwnerControlEffect(UUID exiledCardId,
                                                                    boolean enterTapped,
                                                                    boolean underAbilityController)
        implements CardEffect {

    public ReturnExiledCardToBattlefieldUnderOwnerControlEffect(UUID exiledCardId, boolean enterTapped) {
        this(exiledCardId, enterTapped, false);
    }

    public ReturnExiledCardToBattlefieldUnderOwnerControlEffect(UUID exiledCardId) {
        this(exiledCardId, false);
    }
}
