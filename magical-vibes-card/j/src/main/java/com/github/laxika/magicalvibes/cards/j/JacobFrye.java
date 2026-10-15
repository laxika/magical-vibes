package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "27")
@CardRegistration(set = "ACR", collectorNumber = "132")
public class JacobFrye extends Card {

    private static final CardAnyOfPredicate ASSASSIN_OR_FREERUNNING = new CardAnyOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.ASSASSIN),
            new CardKeywordPredicate(Keyword.FREERUNNING)));

    public JacobFrye() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new Fixed(1),
                        new CardNamedPredicate("Evie Frye"),
                        LibrarySearchDestination.HAND,
                        LibrarySearchPlayer.TARGET_PLAYER),
                "Put Evie Frye into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
        target(new GraveyardCardPredicateTargetFilter(ASSASSIN_OR_FREERUNNING, ownGraveyard), 0, 1);
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN),
                        new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                                ASSASSIN_OR_FREERUNNING, ownGraveyard),
                        false,
                        true));
    }
}
