package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Board-wide static: creatures with the selected landwalk ability can be blocked as though they
 * didn't have it. A {@code null} keyword selects every landwalk ability, including snow landwalk;
 * a non-null keyword selects only that printed landwalk ability.
 *
 * <p>Only landwalk is suppressed — other defender-condition evasion (Scrapdiver Serpent) keeps
 * working, which is why the landwalk-flavoured records flag themselves via
 * {@link BlockabilityRestrictionEffect#unblockableIfDefenderControlsIsLandwalk()}.
 */
public record LandwalkIgnoredForBlockingEffect(Keyword landwalkKeyword) implements CardEffect {

    public LandwalkIgnoredForBlockingEffect() {
        this(null);
    }

    public LandwalkIgnoredForBlockingEffect {
        if (landwalkKeyword != null && !Keyword.LANDWALK_MAP.containsKey(landwalkKeyword)) {
            throw new IllegalArgumentException("Not a landwalk keyword: " + landwalkKeyword);
        }
    }

    public boolean ignoresAllLandwalk() {
        return landwalkKeyword == null;
    }
}
