package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "217")
public class SorinVengefulBloodlord extends Card {

    private static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsPlaneswalkerPredicate()));

    public SorinVengefulBloodlord() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Set.of(Keyword.LIFELINK), GrantScope.OWN_PERMANENTS,
                        CREATURE_OR_PLANESWALKER)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "+2: Sorin deals 1 damage to target player or planeswalker."));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .requiresManaValueEqualsX(true)
                        .grantSubtype(CardSubtype.VAMPIRE)
                        .build()),
                "-X: Return target creature card with mana value X from your graveyard to the battlefield. "
                        + "That creature is a Vampire in addition to its other types.",
                null
        ));
    }
}
