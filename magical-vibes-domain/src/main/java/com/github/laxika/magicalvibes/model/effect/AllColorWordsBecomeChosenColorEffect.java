package com.github.laxika.magicalvibes.model.effect;

/**
 * Global text-changing effect (CR 612.1, applied in layer 3 per CR 613.1c): every color word in
 * the text of every spell and permanent is changed to the source's chosen color word. The chosen
 * color is stored on the source at runtime via
 * {@link com.github.laxika.magicalvibes.model.Permanent#getChosenColor()} (pair with
 * {@link ChooseColorOnEnterEffect}). Used by Swirl the Mists.
 *
 * <p>Unlike the targeted one-shot {@link ChangeColorTextEffect}, this records no
 * {@link com.github.laxika.magicalvibes.model.TextReplacement} on any object: the substitution is
 * derived from the battlefield on every text-change query, so it starts and stops applying as this
 * permanent enters and leaves. It therefore has no static handler — the engine consumes it
 * directly in {@code TextChangeTransformer}.
 */
public record AllColorWordsBecomeChosenColorEffect() implements CardEffect {
}
