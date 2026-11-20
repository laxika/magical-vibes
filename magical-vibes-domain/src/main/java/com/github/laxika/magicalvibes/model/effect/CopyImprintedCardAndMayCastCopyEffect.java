package com.github.laxika.magicalvibes.model.effect;

/**
 * "Copy the exiled card. You may cast the copy [without paying its mana cost]": copies cards in exile and offers the
 * controller a may-choice to cast each copy. The card itself stays exiled; a copy that is declined, or that can't be
 * cast, ceases to exist (CR 707.10a).
 *
 * <p>With no {@code cards} subject it copies the source's imprinted card and every other card exiled with the source
 * (Elite Arcanist, Isochron Scepter): a stack entry with a linked exiled card copies that one card (the registration of
 * a delayed ability a spell registers, Surge to Victory, records the card the spell exiled, so the ability keeps
 * copying it even when the same spell is cast again and exiles another card). {@code copyOtherExiledCard} is
 * Spellweaver Helix's "you may copy the other": every card exiled with the source but the first one with the same name
 * as the stack entry's triggering spell, read as the ability resolves (the live spell, else as it last existed on the
 * stack), and nothing when no exiled card has that name. An accepted copy of this form is cast for free through the
 * free-cast queue.
 *
 * <p>With a {@code cards} subject it copies the cards the subject names in exile ({@code of(cardsThisWay(EXILE))}:
 * "exile target instant or sorcery card from your graveyard. Copy it. You may cast the copy"), and an accepted copy is
 * cast as part of the resolution with this effect's cast options: {@code withoutPayingManaCost} (else its costs are
 * paid, X announced as normal), {@code lifeLossOnCast} ("you may cast the copy. If you do, you lose 2 life", Kaervek)
 * and {@code afterSuccessfulCastEffect} ("if you do, convert Soundwave"). Engine handlers that exile and copy cards
 * themselves park their copies with this form as the may-choice's effect.
 */
public record CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost,
                                                     boolean copyOtherExiledCard,
                                                     CardSubject cards,
                                                     boolean withoutPayingManaCost,
                                                     int lifeLossOnCast,
                                                     CardEffect afterSuccessfulCastEffect)
        implements ImprintedCardXCostEffect {

    public CopyImprintedCardAndMayCastCopyEffect {
        if (cards == null && (!withoutPayingManaCost || lifeLossOnCast != 0 || afterSuccessfulCastEffect != null)) {
            throw new IllegalArgumentException("Cast options need a card subject");
        }
        if (cards != null && (requiresImprintedXCost || copyOtherExiledCard)) {
            throw new IllegalArgumentException("A card subject copies no imprinted card");
        }
        if (cards != null && cards.declaresTarget()) {
            throw new IllegalArgumentException("The copy step declares no target: " + cards);
        }
    }

    public CopyImprintedCardAndMayCastCopyEffect() {
        this(true, false);
    }

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost) {
        this(requiresImprintedXCost, false);
    }

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost, boolean copyOtherExiledCard) {
        this(requiresImprintedXCost, copyOtherExiledCard, null, true, 0, null);
    }

    /** "You may copy the other" exiled card than the one with the triggering spell's name (Spellweaver Helix). */
    public static CopyImprintedCardAndMayCastCopyEffect otherExiledCard() {
        return new CopyImprintedCardAndMayCastCopyEffect(false, true);
    }

    /**
     * "Copy it. You may cast the copy without paying its mana cost" over the cards {@code cards} names in exile, usually
     * {@code Subjects.cardsThisWay(StepKind.EXILE)} after the exile step.
     */
    public static CopyImprintedCardAndMayCastCopyEffect of(CardSubject cards) {
        return new CopyImprintedCardAndMayCastCopyEffect(false, false, cards, true, 0, null);
    }

    /** "You may cast the copy" paying its costs ("You still pay its costs", Nashi). */
    public CopyImprintedCardAndMayCastCopyEffect payingItsCosts() {
        return new CopyImprintedCardAndMayCastCopyEffect(requiresImprintedXCost, copyOtherExiledCard, cards, false,
                lifeLossOnCast, afterSuccessfulCastEffect);
    }

    /** "If you do, you lose {@code amount} life" after casting a copy (Kaervek). */
    public CopyImprintedCardAndMayCastCopyEffect losingLifeOnCast(int amount) {
        return new CopyImprintedCardAndMayCastCopyEffect(requiresImprintedXCost, copyOtherExiledCard, cards,
                withoutPayingManaCost, amount, afterSuccessfulCastEffect);
    }

    /** "If you do, [effect]" after casting a copy (Soundwave's "convert Soundwave"). */
    public CopyImprintedCardAndMayCastCopyEffect ifCastThen(CardEffect effect) {
        return new CopyImprintedCardAndMayCastCopyEffect(requiresImprintedXCost, copyOtherExiledCard, cards,
                withoutPayingManaCost, lifeLossOnCast, effect);
    }

    @Override
    public boolean requiresImprintedXCost() {
        return requiresImprintedXCost;
    }
}
