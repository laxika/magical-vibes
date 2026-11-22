package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "237")
public class VraskaTheSilencer extends Card {

    public VraskaTheSilencer() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new MayPayManaEffect("{1}", new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you())
                        .enteringTapped()
                        .enteringWith(new SetCardTypesEffect(Set.of(CardType.ARTIFACT), Subjects.target()),
                                new GrantSubtypeEffect(CardSubtype.TREASURE, Subjects.target(), true),
                                new GrantActivatedAbilityEffect(new ActivatedAbility(true, null,
                                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                        "{T}, Sacrifice this artifact: Add one mana of any color."), Subjects.target())),
                        "Pay {1} to return that creature to the battlefield under your control as a Treasure?"));
    }
}
