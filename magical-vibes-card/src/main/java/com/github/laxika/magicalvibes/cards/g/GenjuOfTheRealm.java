package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "151")
public class GenjuOfTheRealm extends Card {

    public GenjuOfTheRealm() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{2}",
                                List.of(
                                        new AnimatePermanentsEffect(
                                                8, 12, List.of(CardSubtype.SPIRIT), Set.of(Keyword.TRAMPLE)),
                                        new GrantSupertypeUntilEndOfTurnEffect(
                                                CardSupertype.LEGENDARY, GrantScope.SELF)
                                ),
                                "Enchanted land becomes a legendary 8/12 Spirit creature with trample until end of turn. It's still a land."
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Genju of the Realm to your hand?"
                ));
    }
}
