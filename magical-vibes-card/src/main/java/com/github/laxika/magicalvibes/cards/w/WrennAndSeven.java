package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "226")
public class WrennAndSeven extends Card {

    public WrennAndSeven() {
        // +1: Reveal the top four cards of your library. Put all land cards revealed this way into your
        // hand and the rest into your graveyard. (Mulch — chooseCount == lookCount is deterministic.)
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                        4, 4, new CardTypePredicate(CardType.LAND), true)),
                "+1: Reveal the top four cards of your library. Put all land cards revealed this way into your hand and the rest into your graveyard."
        ));

        // 0: Put any number of land cards from your hand onto the battlefield tapped.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(PutCardToBattlefieldEffect.tappedAnyNumber(new CardTypePredicate(CardType.LAND), "land")),
                "0: Put any number of land cards from your hand onto the battlefield tapped."
        ));

        // −3: Create a green Treefolk creature token with reach and "This token's power and toughness are
        // each equal to the number of lands you control."
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect(
                        1, "Treefolk", 0, 0,
                        CardColor.GREEN, List.of(CardSubtype.TREEFOLK),
                        Set.of(Keyword.REACH), Set.of(),
                        Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl))
                )),
                "\u22123: Create a green Treefolk creature token with reach and \"This token's power and toughness are each equal to the number of lands you control.\""
        ));

        // −8: Return all permanent cards from your graveyard to your hand. You get an emblem with
        // "You have no maximum hand size." (GrantPermanentNoMaxHandSizeEffect = rest-of-game, like Praetor's Counsel.)
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsPermanentPredicate())
                                .returnAll(true)
                                .build(),
                        new GrantPermanentNoMaxHandSizeEffect()
                ),
                "\u22128: Return all permanent cards from your graveyard to your hand. You get an emblem with \"You have no maximum hand size.\""
        ));
    }
}
