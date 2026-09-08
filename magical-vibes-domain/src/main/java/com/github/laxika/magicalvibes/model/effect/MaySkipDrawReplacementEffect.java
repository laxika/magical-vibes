package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/**
 * Capability marker for a static effect that may replace its controller's draw with no draw.
 *
 * <p>Draw replacement markers are consumed by {@code DrawService}, where the affected player is
 * offered the replacement choice one draw at a time.
 */
public interface MaySkipDrawReplacementEffect extends CardEffect {

    DrawReplacementKind replacementKind();
}
