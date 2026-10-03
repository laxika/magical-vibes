package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "510")
@CardRegistration(set = "LTC", collectorNumber = "554")
public class NazgLBattleMace extends Card {

    public NazgLBattleMace() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.DEATHTOUCH),
                        GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new SacrificePermanentsEffect(
                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER),
                GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED,
                new StealDyingOpponentPermanentUnlessPaysLifeEffect(3));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
