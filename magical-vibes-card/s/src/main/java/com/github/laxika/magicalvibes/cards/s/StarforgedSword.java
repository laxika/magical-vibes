package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "249")
public class StarforgedSword extends Card {

    public StarforgedSword() {
        addStaticEffect(new GiftEffect(0));
        addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new GiftPromised()),
                TargetOpponentCreatesTokenEffect.gift(fishToken()));

        targetWhenGiftPromised(TargetFilters.creatureYouControl(), 0, 1, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerOptions.DEFAULT.withInterveningIf(new GiftPromised()),
                        new AttachSourceEquipmentToTargetCreatureEffect());

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(1, "Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH),
                Set.of(), Set.of(), true);
    }
}
