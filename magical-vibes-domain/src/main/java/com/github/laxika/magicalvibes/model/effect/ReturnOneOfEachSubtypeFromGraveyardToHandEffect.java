package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Returns one card of each specified subtype from the controller's graveyard to their hand.
 * Each subtype is processed sequentially — the controller chooses one matching card per subtype.
 * If no card of a given subtype exists in the graveyard, that subtype is skipped.
 * A card chosen for one subtype is removed from the graveyard before the next subtype is processed,
 * so a multi-typed card (e.g. a Pirate Vampire) can only satisfy one subtype.
 *
 * <p>Used by Grim Captain's Call (XLN #108): "Return a Pirate card from your graveyard to your hand,
 * then do the same for Vampire, Dinosaur, and Merfolk."</p>
 *
 * @param subtypes the ordered list of subtypes to return, one card per subtype
 */
public record ReturnOneOfEachSubtypeFromGraveyardToHandEffect(
        List<CardSubtype> subtypes
) implements CardEffect {
}
