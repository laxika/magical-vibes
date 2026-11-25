package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "166")
public class ElvenBow extends Card {

    public ElvenBow() {
        CreateTokenEffect elfWarrior = new CreateTokenEffect(
                "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.youMayPay("{2}", SequenceEffect.of(elfWarrior,
                                new AttachMatchingEquipmentToCreatedPermanentEffect(
                                        new PermanentIsSourcePermanentPredicate()))).withPrompt("Pay {2} to create an Elf Warrior token and attach Elven Bow to it?"));
        addStaticEffect(new AttachedBoostEffect(new Fixed(1), new Fixed(2), Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
