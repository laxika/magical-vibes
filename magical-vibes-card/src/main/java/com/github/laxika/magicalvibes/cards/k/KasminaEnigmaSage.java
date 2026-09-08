package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "196")
public class KasminaEnigmaSage extends Card {

    public KasminaEnigmaSage() {
        ActivatedAbility scryAbility = new ActivatedAbility(
                +2,
                List.of(new ScryEffect(1)),
                "+2: Scry 1."
        );
        ActivatedAbility fractalAbility = ActivatedAbility.variableLoyaltyAbility(
                List.of(new CreateXTokenWithXCountersEffect(
                        "Fractal", 0, 0,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                        List.of(CardSubtype.FRACTAL), CounterType.PLUS_ONE_PLUS_ONE)),
                "−X: Create a 0/0 green and blue Fractal creature token. Put X +1/+1 counters on it.",
                null
        );
        ActivatedAbility searchAbility = new ActivatedAbility(
                -8,
                List.of(new SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffect()),
                "−8: Search your library for an instant or sorcery card that shares a color with this planeswalker, "
                        + "exile that card, then shuffle. You may cast that card without paying its mana cost."
        );

        addActivatedAbility(scryAbility);
        addActivatedAbility(fractalAbility);
        addActivatedAbility(searchAbility);

        var otherPlaneswalkers = new PermanentAllOfPredicate(List.of(
                new PermanentIsPlaneswalkerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                scryAbility, GrantScope.OWN_PERMANENTS, otherPlaneswalkers));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                fractalAbility, GrantScope.OWN_PERMANENTS, otherPlaneswalkers));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                searchAbility, GrantScope.OWN_PERMANENTS, otherPlaneswalkers));
    }
}
