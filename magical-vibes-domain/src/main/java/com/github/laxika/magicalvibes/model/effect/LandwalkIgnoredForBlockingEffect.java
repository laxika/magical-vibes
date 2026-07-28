package com.github.laxika.magicalvibes.model.effect;

/**
 * Board-wide static: creatures with landwalk abilities (CR 702.14a) can be blocked as though they
 * didn't have those abilities. Applies to every creature on the battlefield regardless of
 * controller, and shuts off both printed landwalk {@code Keyword}s and the snow-landwalk shapes
 * modelled as "can't be blocked while the defending player controls …" (Staff of the Ages).
 *
 * <p>Only landwalk is suppressed — other defender-condition evasion (Scrapdiver Serpent) keeps
 * working, which is why the landwalk-flavoured records flag themselves via
 * {@link BlockabilityRestrictionEffect#unblockableIfDefenderControlsIsLandwalk()}.
 */
public record LandwalkIgnoredForBlockingEffect() implements CardEffect {
}
