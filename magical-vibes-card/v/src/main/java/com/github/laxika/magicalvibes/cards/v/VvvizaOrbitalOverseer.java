package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "28")
public class VvvizaOrbitalOverseer extends Card {

    private static final CreateTokenEffect LANDER = CreateTokenEffect.ofArtifactToken(
            1, "Lander", List.of(CardSubtype.LANDER), List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(
                            new SacrificeSelfCost(),
                            new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                    LibrarySearchDestination.BATTLEFIELD_TAPPED)
                    ),
                    "{2}, {T}, Sacrifice this token: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
            )));

    private static final AnimatePermanentsEffect ANIMATE_LANDER = new AnimatePermanentsEffect(
            new Fixed(2), new Fixed(1), List.of(), Set.of(Keyword.FLYING), null, Set.of(CardType.CREATURE),
            GrantScope.TOKENS_CREATED_THIS_RESOLUTION, EffectDuration.PERMANENT, null);

    public VvvizaOrbitalOverseer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, DraftFromSpellbookEffect.conjureOntoBattlefield(List.of(
                AdagiaWindsweptBastion::new,
                EvendoWakingHaven::new,
                KavaronMemorialWorld::new,
                SusurSecundiVoidAltar::new,
                UthrosTitanicGodcore::new)));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, LANDER);
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, ANIMATE_LANDER);
    }
}
