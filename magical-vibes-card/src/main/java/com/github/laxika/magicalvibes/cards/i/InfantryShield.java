package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "35")
@CardRegistration(set = "TDC", collectorNumber = "75")
public class InfantryShield extends Card {

    public InfantryShield() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        SequenceEffect.of(
                                new CreateTokenEffect(CardType.CREATURE, new SourcePower(), "Warrior", 1, 1,
                                        CardColor.RED, null, List.of(CardSubtype.WARRIOR), Set.of(), Set.of(),
                                        true, false, Map.of(), List.of(), false, false, false, 0, Set.of()),
                                new SacrificeCreatedPermanentsAtEndStepEffect()),
                        GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
