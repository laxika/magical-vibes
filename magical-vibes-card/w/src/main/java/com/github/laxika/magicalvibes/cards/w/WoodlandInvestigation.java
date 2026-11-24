package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "21")
public class WoodlandInvestigation extends Card {

    public WoodlandInvestigation() {
        addSpellEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addSpellEffect(new PerpetuallyGrantCardCharacteristicsEffect(
                Set.of(CardType.ARTIFACT),
                Set.of(CardSubtype.CLUE),
                List.of(new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                        "{2}, Sacrifice this permanent: Draw a card.")))
                .to(Subjects.foundThisWay()));
    }
}
