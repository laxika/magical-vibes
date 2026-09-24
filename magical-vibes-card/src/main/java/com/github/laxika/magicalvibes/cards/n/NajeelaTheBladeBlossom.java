package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1032")
@CardRegistration(set = "SLD", collectorNumber = "1557")
@CardRegistration(set = "MAR", collectorNumber = "24")
@CardRegistration(set = "FCA", collectorNumber = "42")
@CardRegistration(set = "OMB", collectorNumber = "24")
public class NajeelaTheBladeBlossom extends Card {

    public NajeelaTheBladeBlossom() {
        MayEffect createToken = new MayEffect(
                new CreateTokenForTriggeringPlayerEffect(
                        new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.WARRIOR), true)),
                "Have its controller create a 1/1 white Warrior creature token that's tapped and attacking?");
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR), createToken));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()),
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.LIFELINK, Keyword.HASTE),
                                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)
                ),
                "{W}{U}{B}{R}{G}: Untap all attacking creatures. They gain trample, lifelink, and haste until end of turn. After this phase, there is an additional combat phase. Activate only during combat.",
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ));
    }
}
