package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "581")
public class GisasFavoriteShovel extends Card {

    public GisasFavoriteShovel() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature attacks, defending player sacrifices a creature of their
        // choice. If they do, create a Walker token.
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new SacrificePermanentsEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                        SacrificeRecipient.DEFENDING_PLAYER)
                        .withRecordedSacrificeCount(),
                ConditionalEffect.unless(
                        new EventValueAtLeast(1),
                        new CreateTokenEffect(1, "Walker", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()))));

        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
