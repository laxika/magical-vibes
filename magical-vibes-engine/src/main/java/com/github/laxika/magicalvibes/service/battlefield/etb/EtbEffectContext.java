package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Per-trigger context passed to {@link EtbEffectResolver} handlers when a creature's own
 * enter-the-battlefield ({@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ENTER_BATTLEFIELD})
 * effects are resolved at trigger time.
 *
 * <p>Carries everything a handler may need to unwrap modal choices, materialise values, or evaluate
 * an intervening-if gate (CR 603.4):
 *
 * @param gameData        the current game state
 * @param card            the entering card whose ETB abilities are being resolved
 * @param controllerId    the controller of the entering permanent (the ability controller)
 * @param wasCastFromHand whether the permanent was cast from hand (gates cast-from-hand effects)
 * @param etbMode         the modal-choice index selected at cast time (for {@code ChooseOneEffect})
 * @param kicked          whether the spell was kicked (gates kicked-conditional effects)
 * @param evoked          whether the spell was cast for its evoke cost (gates the evoke sacrifice)
 */
public record EtbEffectContext(GameData gameData, Card card, UUID controllerId,
                               boolean wasCastFromHand, int etbMode, boolean kicked, boolean evoked) {}
