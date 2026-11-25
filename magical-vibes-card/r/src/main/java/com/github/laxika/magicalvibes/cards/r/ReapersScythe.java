package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "48")
@CardRegistration(set = "FIC", collectorNumber = "117")
public class ReapersScythe extends Card {

    public ReapersScythe() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                PutCountersEffect.onSelf(CounterType.SOUL, new PlayersWhoLostLifeThisTurn()));
        addStaticEffect(new AttachedBoostEffect(
                new CountersOnSource(CounterType.SOUL),
                new CountersOnSource(CounterType.SOUL),
                Subjects.attachedCreature()));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.ASSASSIN, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
