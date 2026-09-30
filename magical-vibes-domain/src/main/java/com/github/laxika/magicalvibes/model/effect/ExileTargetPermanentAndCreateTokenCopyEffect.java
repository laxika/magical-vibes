package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Exiles the targeted permanent, then creates a token copy of the exiled permanent. By default the
 * token is created for the spell's controller and exiled at the beginning of the next end step.
 * Optional copy overrides support effects such as Kaya's Spirit token and Dedicated Dollmaker's
 * copy for the exiled permanent's controller.
 */
public record ExileTargetPermanentAndCreateTokenCopyEffect(
        List<CardSubtype> additionalSubtypes,
        Set<CardType> additionalTypes,
        Set<Keyword> additionalKeywords,
        CardColor colorOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        boolean skipTokenCopyIfAura,
        boolean createForTargetController,
        boolean exileTokenAtEndStep,
        boolean removeLegendary
) implements RemovalEffect {

    public ExileTargetPermanentAndCreateTokenCopyEffect() {
        this(List.of(), Set.of(), Set.of(), null, null, null, false, false, true, false);
    }

    public ExileTargetPermanentAndCreateTokenCopyEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Set<Keyword> additionalKeywords,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            boolean skipTokenCopyIfAura) {
        this(additionalSubtypes, additionalTypes, additionalKeywords, colorOverride, powerOverride,
                toughnessOverride, skipTokenCopyIfAura, false, true, false);
    }

    public ExileTargetPermanentAndCreateTokenCopyEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Set<Keyword> additionalKeywords,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            boolean skipTokenCopyIfAura,
            boolean createForTargetController,
            boolean exileTokenAtEndStep,
            boolean removeLegendary) {
        this.additionalSubtypes = List.copyOf(additionalSubtypes);
        this.additionalTypes = Set.copyOf(additionalTypes);
        this.additionalKeywords = Set.copyOf(additionalKeywords);
        this.colorOverride = colorOverride;
        this.powerOverride = powerOverride;
        this.toughnessOverride = toughnessOverride;
        this.skipTokenCopyIfAura = skipTokenCopyIfAura;
        this.createForTargetController = createForTargetController;
        this.exileTokenAtEndStep = exileTokenAtEndStep;
        this.removeLegendary = removeLegendary;
    }

    /** Creates a lasting, nonlegendary copy for the controller of the exiled permanent. */
    public static ExileTargetPermanentAndCreateTokenCopyEffect forTargetController(
            Set<CardType> additionalTypes, boolean removeLegendary) {
        return new ExileTargetPermanentAndCreateTokenCopyEffect(
                List.of(), additionalTypes, Set.of(), null, null, null,
                false, true, false, removeLegendary);
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
