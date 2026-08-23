package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TOR", collectorNumber = "98")
public class FlamingGambit extends Card {

    public FlamingGambit() {
        XValue damage = new XValue();
        DealDamageToTargetPlayerOrPlaneswalkerEffect playerDamage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(damage);

        addCastingOption(new FlashbackCast("{X}{R}{R}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new TargetPlayerControlsPermanent(new PermanentIsCreaturePredicate()),
                playerDamage,
                new MayEffect(
                        new DealDamageToTargetCreatureDamagedPlayerControlsEffect(damage, true),
                        "Have Flaming Gambit deal its damage to a creature instead?",
                        playerDamage,
                        MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER)));
    }
}
