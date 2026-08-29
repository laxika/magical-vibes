package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Exiles the targeted permanent, then creates a token copy of the exiled permanent under the
 * spell's controller. The token is exiled at the beginning of the next end step. Optional copy
 * overrides support effects such as Kaya's Spirit token.
 */
public record ExileTargetPermanentAndCreateTokenCopyEffect(
        List<CardSubtype> additionalSubtypes,
        Set<CardType> additionalTypes,
        Set<Keyword> additionalKeywords,
        CardColor colorOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        boolean skipTokenCopyIfAura
) implements RemovalEffect {

    public ExileTargetPermanentAndCreateTokenCopyEffect() {
        this(List.of(), Set.of(), Set.of(), null, null, null, false);
    }

    public ExileTargetPermanentAndCreateTokenCopyEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Set<Keyword> additionalKeywords,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            boolean skipTokenCopyIfAura) {
        this.additionalSubtypes = List.copyOf(additionalSubtypes);
        this.additionalTypes = Set.copyOf(additionalTypes);
        this.additionalKeywords = Set.copyOf(additionalKeywords);
        this.colorOverride = colorOverride;
        this.powerOverride = powerOverride;
        this.toughnessOverride = toughnessOverride;
        this.skipTokenCopyIfAura = skipTokenCopyIfAura;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
