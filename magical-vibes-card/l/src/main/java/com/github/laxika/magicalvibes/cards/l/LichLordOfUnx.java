package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "24")
public class LichLordOfUnx extends Card {

    public LichLordOfUnx() {
        // {U}{B}, {T}: Create a 1/1 blue and black Zombie Wizard creature token.
        addActivatedAbility(new ActivatedAbility(
                true, "{U}{B}",
                List.of(new CreateTokenEffect(
                        "Zombie Wizard", 1, 1, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.ZOMBIE, CardSubtype.WIZARD))),
                "{U}{B}, {T}: Create a 1/1 blue and black Zombie Wizard creature token."));

        // {U}{U}{B}{B}: Target player loses X life and mills X cards, where X is the
        // number of Zombies you control. Both parts act on the ability's targeted player.
        PermanentCount zombiesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false, "{U}{U}{B}{B}",
                List.of(
                        new LoseLifeEffect(zombiesYouControl, LoseLifeRecipient.TARGET_PLAYER),
                        new MillEffect(zombiesYouControl, MillRecipient.TARGET_PLAYER)
                ),
                "{U}{U}{B}{B}: Target player loses X life and mills X cards, where X is the number of Zombies you control.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player")));
    }
}
