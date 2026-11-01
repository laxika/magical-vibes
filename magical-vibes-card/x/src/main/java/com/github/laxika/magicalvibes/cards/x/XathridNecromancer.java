package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "123")
@CardRegistration(set = "PIO", collectorNumber = "120")
@CardRegistration(set = "C20", collectorNumber = "141")
public class XathridNecromancer extends Card {

    public XathridNecromancer() {
        // "This creature" is its own "when this dies" ability, so the Human restriction applies only to another
        // creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.HUMAN),
                tappedZombie()
        ));
        addTrigger(Triggers.dies(ObjectRelation.THIS), tappedZombie());
    }

    private static CreateTokenEffect tappedZombie() {
        return new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.<Keyword>of(), Set.<CardType>of(), true);
    }
}
