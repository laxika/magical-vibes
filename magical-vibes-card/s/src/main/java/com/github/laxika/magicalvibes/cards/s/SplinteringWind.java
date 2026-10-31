package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "99")
public class SplinteringWind extends Card {

    public SplinteringWind() {
        // The Splinter token carries its own cumulative upkeep. "When it leaves the battlefield" is a
        // delayed triggered ability of this ability (CR 603.7), so "you" stays the activating player
        // even if the token has changed controllers.
        List<Ability> splinterEffects = List.of(LegacySlots.triggered(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{G}")));
        CardEffect splinterLeaves = SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                new MassDamageEffect(1, false, false, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()))));

        CreateTokenEffect splinter = new CreateTokenEffect(1, "Splinter", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SPLINTER), Set.of(Keyword.FLYING), Set.of(), splinterEffects);

        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new DealDamageToTargetCreatureEffect(1),
                        new CreateTokenAndLinkToSourceEffect(splinter, false, splinterLeaves)),
                "{2}{G}: Splintering Wind deals 1 damage to target creature. Create a 1/1 green "
                        + "Splinter creature token. It has flying and \"Cumulative upkeep {G}.\" When it "
                        + "leaves the battlefield, it deals 1 damage to you and each creature you control."));
    }
}
