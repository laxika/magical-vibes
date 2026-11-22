package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevents all damage that artifact sources would deal to this permanent (Argothian Treefolk), or with
 * {@link Subjects#attachedCreature()} to the enchanted creature (Artifact Ward).
 *
 * @param subject whose damage is prevented: {@link Subjects#source()} or the attachment's host
 *                ({@link Subjects#attachedCreature()})
 */
public record PreventArtifactDamageToSelfEffect(PermanentSubject subject) implements CardEffect {

    public PreventArtifactDamageToSelfEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("Protects the source or the attached permanent: " + subject);
        }
    }

    public PreventArtifactDamageToSelfEffect() {
        this(Subjects.source());
    }
}
