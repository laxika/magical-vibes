package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "217")
public class WrennAndRealmbreaker extends Card {

    private static final String EMBLEM_TEXT =
            "You may play lands and cast permanent spells from your graveyard.";

    public WrennAndRealmbreaker() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect()),
                        "{T}: Add one mana of any color."),
                GrantScope.OWN_LANDS));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new AnimatePermanentsEffect(
                        3, 3, List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.VIGILANCE, Keyword.HEXPROOF, Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Up to one target land you control becomes a 3/3 Elemental creature with vigilance, "
                        + "hexproof, and haste until your next turn. It's still a land.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.landYouControl()),
                0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new MillControllerAndMayReturnMilledPermanentToHandEffect(3)),
                "-2: Mill three cards. You may put a permanent card from among the milled cards into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new PlayLandsFromGraveyardEffect(),
                                new CastSpellsFromGraveyardEffect(new CardIsPermanentPredicate())),
                        EMBLEM_TEXT)),
                "-7: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
