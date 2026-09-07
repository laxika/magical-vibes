package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "Choose one that hasn't been chosen —" (Demonic Pact). A modal triggered ability whose modes are
 * consumed: every mode this permanent has already resolved is removed from the offered list, so the
 * final mode is forced once the other three are gone.
 *
 * <p>Unlike {@link ChooseOneEffect} in a trigger slot (mode picked as the ability resolves), the
 * mode here is chosen as the ability is put on the stack, together with that mode's targets, which
 * is what the rules require of a modal triggered ability. The already-chosen labels live on the
 * {@code Permanent} ({@code chosenModeLabels}), so a new object (a re-entering copy) starts over.
 */
public record ChooseModeNotYetChosenEffect(List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {
}
