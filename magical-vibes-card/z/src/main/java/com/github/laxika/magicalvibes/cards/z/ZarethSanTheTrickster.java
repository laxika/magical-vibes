package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardIsPermanentPredicate(), false));
    }
}
