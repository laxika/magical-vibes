package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "184")
public class QasaliAmbusher extends Card {

    public QasaliAmbusher() {
        // Reach is auto-loaded from Scryfall.

        // If a creature is attacking you and you control a Forest and a Plains, you may cast this
        // creature without paying its mana cost (empty cost list) and as though it had flash.
        addCastingOption(new AlternateHandCast(
                List.of(),
                new AllConditions(List.of(
                        new CreatureAttackingController(),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)))),
                true));
    }
}
