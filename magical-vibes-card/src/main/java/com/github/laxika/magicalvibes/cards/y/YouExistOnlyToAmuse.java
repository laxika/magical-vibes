package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "361")
public class YouExistOnlyToAmuse extends Card {

    public YouExistOnlyToAmuse() {
        Map<EffectSlot, CardEffect> devilTokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        CreateTokenEffect createDevils = new CreateTokenEffect(
                3, "Devil", 1, 1, CardColor.RED,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects);

        ControlsPermanentCount sixLands = new ControlsPermanentCount(6, new PermanentIsLandPredicate());
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create three 1/1 red Devil creature tokens",
                        createDevils),
                new ChooseOneEffect.ChooseOneOption(
                        "Opposing creatures have base power and toughness 1/1 and lose all abilities until your next turn",
                        List.of(
                                new SetBasePowerToughnessEffect(1, 1, GrantScope.OPPONENT_CREATURES,
                                        EffectDuration.UNTIL_YOUR_NEXT_TURN),
                                new LosesAllAbilitiesEffect(GrantScope.OPPONENT_CREATURES,
                                        EffectDuration.UNTIL_YOUR_NEXT_TURN)))), sixLands));
    }
}
