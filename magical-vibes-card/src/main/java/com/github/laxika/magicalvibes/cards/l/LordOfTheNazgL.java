package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromRingBearersEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "60")
@CardRegistration(set = "LTC", collectorNumber = "142")
public class LordOfTheNazgL extends Card {

    public LordOfTheNazgL() {
        PermanentHasSubtypePredicate wraiths = new PermanentHasSubtypePredicate(CardSubtype.WRAITH);

        // Wraiths you control have protection from Ring-bearers.
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromRingBearersEffect(), GrantScope.ALL_OWN_CREATURES, wraiths));

        // Whenever you cast an instant or sorcery spell, create a 3/3 black Wraith creature token
        // with menace. Then if you control nine or more Wraiths, set their base P/T to 9/9 until
        // end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(
                        new CreateTokenEffect("Wraith", 3, 3, CardColor.BLACK,
                                List.of(CardSubtype.WRAITH), Set.of(Keyword.MENACE), Set.of()),
                        new ConditionalEffect(
                                new ControlsPermanentCount(9, wraiths),
                                new SetAllOwnCreaturesBasePowerToughnessEffect(9, 9, wraiths)))));
    }
}
