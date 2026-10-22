package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NinjutsuEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "242")
public class ZarethSanTheTrickster extends Card {

    public ZarethSanTheTrickster() {
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{U}{B}",
                List.of(new NinjutsuEffect()),
                "{2}{U}{B}, Return an unblocked attacking Rogue you control to its owner's hand: "
                        + "Put this card from your hand onto the battlefield tapped and attacking.")
                .withNinjutsu(new PermanentAllOfPredicate(List.of(
                        new PermanentIsUnblockedAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ROGUE)))));

        // "Whenever Zareth San deals combat damage to a player, you may put target permanent card from that
        // player's graveyard onto the battlefield under your control." The target is chosen as the trigger
        // goes on the stack; the "you may" is decided on resolution.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardIsPermanentPredicate(), false),
                        "Put the chosen card from that player's graveyard onto the battlefield under your control?"));
    }
}
