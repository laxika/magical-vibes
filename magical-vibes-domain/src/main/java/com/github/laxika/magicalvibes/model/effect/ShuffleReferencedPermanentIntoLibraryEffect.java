package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles the permanent {@code subject} names into its owner's library, without targeting it. The default subject
 * is {@link Subjects#attached()}: "shuffle enchanted creature into its owner's library" (Dramatic Accusation, Watery
 * Grasp, Stay Hidden, Stay Silent), the creature the Aura enchants as the ability resolves, else as the Aura last
 * existed (CR 608.2h).
 */
public record ShuffleReferencedPermanentIntoLibraryEffect(PermanentSubject subject)
        implements CardEffect {

    public ShuffleReferencedPermanentIntoLibraryEffect {
        if (subject == null) {
            subject = Subjects.attached();
        }
        if (subject.declaresTarget()) {
            throw new IllegalArgumentException("A target is ShuffleTargetPermanentIntoLibraryEffect's: " + subject);
        }
    }

    public ShuffleReferencedPermanentIntoLibraryEffect() {
        this(Subjects.attached());
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
