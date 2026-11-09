package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, returns the land cards the trigger event was about from their owner's graveyard to the battlefield
 * under the ability controller's control, each only if it is still the graveyard object the event named (CR 400.7).
 * The controller of the triggered ability is the graveyard owner, so a land returns under its owner's control.
 *
 * <p>The lands are the stack entry's triggering cards (with their graveyard entry versions) when it has any, otherwise its
 * triggering card (with {@link com.github.laxika.magicalvibes.model.StackEntry#isTriggeringCardIncarnation}). Used by
 * Sacred Ground ("Whenever a spell or ability an opponent controls causes a land to be put into your graveyard from
 * the battlefield, return that card to the battlefield") and Hedge Shredder ("Whenever one or more land cards are put
 * into your graveyard from your library, put them onto the battlefield tapped").
 *
 * @param enterTapped whether the lands enter the battlefield tapped
 */
public record ReturnTriggeringLandFromGraveyardToBattlefieldEffect(boolean enterTapped) implements CardEffect {

    public ReturnTriggeringLandFromGraveyardToBattlefieldEffect() {
        this(false);
    }
}
