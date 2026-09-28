package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "6")
@CardRegistration(set = "LCC", collectorNumber = "28")
public class DonAndresTheRenegade extends Card {

    public DonAndresTheRenegade() {
        PermanentNotPredicate controlledButNotOwned = new PermanentNotPredicate(
                new PermanentOwnedBySourceControllerPredicate());

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2, 2, Set.of(Keyword.MENACE, Keyword.DEATHTOUCH),
                GrantScope.ALL_OWN_CREATURES, controlledButNotOwned));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.PIRATE, GrantScope.ALL_OWN_CREATURES, false, controlledButNotOwned));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardControllerDoesNotOwnPredicate())),
                List.of(CreateTokenEffect.ofTappedTreasureToken(2))));
    }
}
