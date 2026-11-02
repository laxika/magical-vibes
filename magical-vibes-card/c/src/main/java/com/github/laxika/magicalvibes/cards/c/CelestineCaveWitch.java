package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "37")
public class CelestineCaveWitch extends Card {

    public CelestineCaveWitch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, 2, "Insect", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.INSECT), Set.of(), Set.of(), false, false,
                List.of(), List.of(), false, false, false, 0, Set.of()));
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.INSECT)),
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.INSECT),
                                new CreateTokenAttachedToDefendingPlayerEffect(curseToken()),
                                "an Insect"),
                        "Sacrifice an Insect?")));
    }

    private static CreateTokenEffect curseToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT, 1, "Curse", 0, 0, CardColor.BLACK, null,
                List.of(CardSubtype.AURA, CardSubtype.CURSE), Set.of(), Set.of(), false, false,
                List.of(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER))),
                List.of(), false, false, false, 0, Set.<Keyword>of());
    }
}
