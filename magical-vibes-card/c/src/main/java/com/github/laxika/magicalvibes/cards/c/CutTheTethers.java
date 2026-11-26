package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Cut the Tethers — "For each Spirit, return it to its owner's hand unless that player pays {3}."
 *
 * <p>"That player" is the owner the sentence just named, so a Spirit that has changed hands is kept or lost by the
 * player whose hand it would return to.
 */
@CardRegistration(set = "CHK", collectorNumber = "56")
public class CutTheTethers extends Card {

    public CutTheTethers() {
        PermanentSubject spirits = Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT));
        addSpellEffect(UnlessPaysEffect.forEach(spirits, Subjects.ownerOf(spirits), new PayManaCost("{3}"),
                ReturnToHandEffect::new));
    }
}
