package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "205")
@CardRegistration(set = "SLD", collectorNumber = "1138")
@CardRegistration(set = "TSR", collectorNumber = "386")
@CardRegistration(set = "CMM", collectorNumber = "356")
@CardRegistration(set = "CMM", collectorNumber = "686")
public class Slimefoot extends Card {

    public Slimefoot() {
        // Whenever a Saproling you control dies, Slimefoot, the Stowaway deals 1 damage to each
        // opponent and you gain 1 life. This is one triggered ability, so both steps share one stack
        // entry. A Slimefoot that is a Saproling as it dies sees its own death (CR 603.10a look-back).
        var saproling = new PermanentHasSubtypePredicate(CardSubtype.SAPROLING);
        addTrigger(Triggers.creatureDies(saproling, ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));

        // {4}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(false, "{4}",
                List.of(new CreateTokenEffect("Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{4}: Create a 1/1 green Saproling creature token."));
    }
}
