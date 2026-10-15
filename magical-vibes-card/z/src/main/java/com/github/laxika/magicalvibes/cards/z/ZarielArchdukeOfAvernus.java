package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "172")
public class ZarielArchdukeOfAvernus extends Card {

    private static final String EMBLEM_TEXT =
            "At the end of the first combat phase on your turn, untap target creature you control. "
                    + "After this phase, there is an additional combat phase.";

    public ZarielArchdukeOfAvernus() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES)),
                "+1: Creatures you control get +1/+0 and gain haste until end of turn."));

        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        1, "Devil", 1, 1, CardColor.RED,
                        List.of(CardSubtype.DEVIL), Set.of(), Set.of(), tokenEffects)),
                "0: Create a 1/1 red Devil creature token with \"When this token dies, it deals 1 damage to any target.\""));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.END_OF_FIRST_COMBAT,
                                List.of(
                                        new UntapPermanentsEffect(
                                                TapUntapScope.TARGET,
                                                new PermanentAllOfPredicate(List.of(
                                                        new PermanentIsCreaturePredicate(),
                                                        new PermanentControlledBySourceControllerPredicate()))),
                                        new AdditionalCombatPhaseEffect(1)),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\""));
    }
}
