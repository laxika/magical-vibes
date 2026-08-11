package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsHandsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "188")
public class AshiokNightmareWeaver extends Card {

    public AshiokNightmareWeaver() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ExileTopCardsToSourceEffect(
                        3, false, false, LibraryScope.TARGET_OPPONENT, true)),
                "+2: Exile the top three cards of target opponent's library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent"
                )
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new ReturnCardExiledWithSourceToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), true, CardSubtype.NIGHTMARE)),
                "\u2212X: Put a creature card with mana value X exiled with Ashiok onto the battlefield "
                        + "under your control. That creature is a Nightmare in addition to its other types.",
                null
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new ExileAllOpponentsHandsEffect(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)),
                "\u221210: Exile all cards from all opponents' hands and graveyards."
        ));
    }
}
