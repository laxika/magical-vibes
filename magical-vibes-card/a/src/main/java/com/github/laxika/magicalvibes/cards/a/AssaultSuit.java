package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "369")
@CardRegistration(set = "C14", collectorNumber = "53")
@CardRegistration(set = "C16", collectorNumber = "242")
@CardRegistration(set = "40K", collectorNumber = "230")
@CardRegistration(set = "CMA", collectorNumber = "208")
public class AssaultSuit extends Card {

    public AssaultSuit() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()));
        addStaticEffect(new EnchantedCreatureCantAttackControllerEffect());
        addStaticEffect(new AttachedPermanentRestrictionEffect(new CantBeSacrificedEffect()));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new MayEffect(
                SequenceEffect.of(
                        new TargetPlayerGainsControlOfSourceCreatureEffect(Subjects.targetPlayerDeclaredElsewhere(),
                                Duration.UNTIL_END_OF_TURN, Subjects.attached()),
                        new UntapPermanentsEffect(Subjects.attached())),
                "Have that player gain control of equipped creature until end of turn?"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
